package com.stars.bigbang.config;

import com.stars.bigbang.entity.Category;
import com.stars.bigbang.entity.Game;
import com.stars.bigbang.entity.Plateform;
import com.stars.bigbang.repositories.CategoryRepository;
import com.stars.bigbang.repositories.GameRepository;
import com.stars.bigbang.repositories.PlateformRepository;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Profile("local")
@Service
public class initPostgre {

    @Autowired
    PlateformRepository plateformrepo;

    @Autowired
    CategoryRepository categoryRepo;

    @Autowired
    GameRepository gameRepo;

    @PostConstruct
    public void initPostgreCateogories(){
        Category action = categoryRepo.save(new Category("Action"));
        Category rpg = categoryRepo.save(new Category("RPG"));
        Category horreur = categoryRepo.save(new Category("Horreur"));
        Category hardcore = categoryRepo.save(new Category("Hardcore"));
    }

    @PostConstruct
    public void  initPostgrePlateform(){
        Plateform pc = plateformrepo.save(new Plateform("PC"));
        Plateform nintendoSwitch = plateformrepo.save(new Plateform("Nintendo switch"));
        Plateform xboxSeriesX = plateformrepo.save(new Plateform("Xbox Series X"));
        Plateform xboxSeriesS = plateformrepo.save(new Plateform("Xbox Series S"));
        Plateform play5 = plateformrepo.save(new Plateform("Playstation 5"));
    }

    @PostConstruct
    public void initPostgreGame(){
        Game theWitcher = gameRepo.save(new Game("The Witcher 3",
                "18",
                Set.of(plateformrepo.findByPlateform("PC")),
                Set.of(categoryRepo.findByCategory("RPG")),
                LocalDateTime.of(2015, 5, 19, 0, 0)));
    }

}

