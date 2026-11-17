package com.stars.bigbang.service;

import com.stars.bigbang.dto.payload.CreateShareLinkDto;
import com.stars.bigbang.dto.response.GamesListDto;
import com.stars.bigbang.dto.response.ShareLinkDto;
import com.stars.bigbang.dto.response.SharedWalletDto;
import com.stars.bigbang.entity.ShareLink;
import com.stars.bigbang.entity.ShareScope;
import com.stars.bigbang.repository.GamesListRepository;
import com.stars.bigbang.repository.ShareLinkRepository;
import com.stars.bigbang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Liens de partage en lecture seule (wiki, page Features, section Sharing).
 * Ce service ne contient aucune méthode qui modifie une liste : la « lecture seule » vient de là.
 */
@Service
@RequiredArgsConstructor
public class ShareService {

    private static final SecureRandom RANDOM = new SecureRandom();
    /** 32 octets aléatoires = 256 bits : impossible à deviner, même en essayant des milliards de valeurs. */
    private static final int TOKEN_BYTES = 32;

    private final ShareLinkRepository shareLinkRepository;
    private final GamesListRepository gamesListRepository;
    private final UserRepository userRepository;
    private final GameService gameService;
    private final CommentaryService commentaryService;
    private final WalletService walletService;

    @Transactional
    public ShareLinkDto create(long userId, CreateShareLinkDto request) {
        ShareLink link = new ShareLink();
        link.setToken(newToken());
        link.setUser(userRepository.getReferenceById(userId));
        link.setScope(request.scope());
        link.setIncludeCommentaries(Boolean.TRUE.equals(request.includeCommentaries()));
        if (request.scope() == ShareScope.LISTS) {
            link.setListIds(ownListIds(request.listIds(), userId));
        }
        return toDto(shareLinkRepository.save(link), gameService.getGamesList(userId));
    }

    @Transactional(readOnly = true)
    public List<ShareLinkDto> findMine(long userId) {
        List<GamesListDto> myLists = gameService.getGamesList(userId);
        return shareLinkRepository.findAllByUserIdOrderByIdDesc(userId).stream()
                .map(link -> toDto(link, myLists))
                .toList();
    }

    /** Révoque le lien : il est supprimé, son URL ne mène plus à rien. */
    @Transactional
    public void revoke(long userId, long linkId) {
        ShareLink link = shareLinkRepository.findByIdAndUserId(linkId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found"));
        shareLinkRepository.delete(link);
    }

    /**
     * Contenu montré au visiteur d'un lien. Un lien inconnu, révoqué, ou dont toutes les listes ont été
     * supprimées répond 404 : on ne dit pas si le lien a existé.
     */
    @Transactional(readOnly = true)
    public SharedWalletDto view(String token) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found"));
        long ownerId = link.getUser().getId();

        List<GamesListDto> lists = gameService.getGamesList(ownerId);
        if (link.getScope() == ShareScope.LISTS) {
            lists = lists.stream().filter(list -> link.getListIds().contains(list.id())).toList();
            if (lists.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found");
            }
        }
        if (link.isIncludeCommentaries()) {
            lists = commentaryService.withCommentaries(lists);
        }
        return new SharedWalletDto(link.getUser().getUsername(), link.getScope(), walletService.backgroundOf(ownerId), lists);
    }

    /** Vérifie que chaque liste demandée appartient bien à l'utilisateur (404 sinon, comme partout ailleurs). */
    private Set<Long> ownListIds(List<Long> requestedIds, long userId) {
        if (requestedIds == null || requestedIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose at least one games list to share");
        }
        Set<Long> listIds = new LinkedHashSet<>(requestedIds);
        for (Long listId : listIds) {
            if (!gamesListRepository.existsByIdAndUserId(listId, userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Games list not found");
            }
        }
        return listIds;
    }

    private String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        // Base64 "URL" : uniquement des caractères utilisables tels quels dans une adresse web
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ShareLinkDto toDto(ShareLink link, List<GamesListDto> ownerLists) {
        List<String> listNames = ownerLists.stream()
                .filter(list -> link.getListIds().contains(list.id()))
                .map(GamesListDto::name)
                .toList();
        return new ShareLinkDto(link.getId(), link.getToken(), link.getScope(), link.isIncludeCommentaries(), listNames, link.getCreatedAt());
    }
}
