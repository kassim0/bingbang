import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {of} from 'rxjs';
import {GamesSearchPopupComponent} from './games-search-popup.component';
import {aGamesList, aSearchResult, commonTestProviders} from '../../testing/test-helpers';
import {GamesList} from '../../models/games.model';

describe('ListGamesPopupComponent', () => {
  let component: GamesSearchPopupComponent;
  let fixture: ComponentFixture<GamesSearchPopupComponent>;
  let http: HttpTestingController;
  let dialogRef: jasmine.SpyObj<MatDialogRef<GamesSearchPopupComponent>>;
  let dialog: jasmine.SpyObj<MatDialog>;

  /** Crée la popup. Sans liste : mode « nouvelle liste ». Avec : mode « ajouter à cette liste ». */
  async function createPopup(gamesList?: GamesList) {
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close', 'backdropClick', 'keydownEvents']);
    dialogRef.backdropClick.and.returnValue(of());
    dialogRef.keydownEvents.and.returnValue(of());
    dialog = jasmine.createSpyObj('MatDialog', ['open']);
    await TestBed.configureTestingModule({
      imports: [GamesSearchPopupComponent],
      providers: [
        ...commonTestProviders,
        {provide: MAT_DIALOG_DATA, useValue: gamesList ? {gamesList} : {}},
        {provide: MatDialogRef, useValue: dialogRef},
        {provide: MatDialog, useValue: dialog},
      ],
    })
    .compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(GamesSearchPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function element(testId: string): HTMLElement | null {
    return (fixture.nativeElement as HTMLElement).querySelector(`[data-testid="${testId}"]`);
  }

  function search(text: string) {
    component.receiveData(text);
    return http.expectOne(request => request.url === '/api/games/search' && request.params.get('name') === text);
  }

  afterEach(() => http.verify());

  it('should create', async () => {
    await createPopup();
    expect(component).toBeTruthy();
  });

  // ---------- recherche ----------

  it('shows the games returned by the search', async () => {
    await createPopup();
    search('zelda').flush([aSearchResult(1, 'Zelda BOTW'), aSearchResult(2, 'Zelda TOTK')]);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('Zelda BOTW');
    expect(text).toContain('Zelda TOTK');
    expect(component.isSearching).toBeFalse();
  });

  it('searches the text typed in the search bar, not the native "search" event of the input', async () => {
    await createPopup();
    const input = element('search-bar-input') as HTMLInputElement;
    input.value = 'zelda';
    input.dispatchEvent(new Event('input'));

    // Entrée dans le champ, puis l'événement natif "search" qu'un <input type="search"> émet et qui remonte
    input.dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter'}));
    input.dispatchEvent(new Event('search', {bubbles: true}));

    http.expectOne(request => request.url === '/api/games/search' && request.params.get('name') === 'zelda').flush([]);
    expect(component.searchedText).toBe('zelda');
  });

  it('stops the spinner and shows an error when the search fails', async () => {
    await createPopup();
    search('zelda').flush('error', {status: 502, statusText: 'Bad Gateway'});
    fixture.detectChanges();

    expect(component.isSearching).toBeFalse();
    expect(element('search-error')).not.toBeNull();
  });

  it('says when no game matches the search', async () => {
    await createPopup();
    search('qwertyuiop').flush([]);
    fixture.detectChanges();

    expect(element('search-no-result')?.textContent).toContain('qwertyuiop');
    expect(element('search-error')).toBeNull();
  });

  it('shows neither an error nor "no result" before the first search', async () => {
    await createPopup();

    expect(element('search-error')).toBeNull();
    expect(element('search-no-result')).toBeNull();
  });

  // ---------- sélection ----------

  it('adds and removes a game from the selection', async () => {
    await createPopup();
    const zelda = aSearchResult(1, 'Zelda');
    const mario = aSearchResult(2, 'Mario');

    component.onAddGame(zelda);
    component.onAddGame(mario);
    component.onRemoveGame(zelda);

    expect(component.addedGames).toEqual([mario]);
  });

  it('flags the games that are already in the list being completed', async () => {
    await createPopup(aGamesList(1, 'Loved', ['Hades']));
    const hadesIgdbId = component.gamesList!.games[0].game.igdbId!;

    expect(component.isAlreadyInList(aSearchResult(hadesIgdbId, 'Hades'))).toBeTrue();
    expect(component.isAlreadyInList(aSearchResult(999, 'Celeste'))).toBeFalse();
    // même numéro mais autre source : ce n'est pas le même jeu
    expect(component.isAlreadyInList({...aSearchResult(hadesIgdbId, 'Other'), source: 'RAWG'})).toBeFalse();
  });

  // ---------- valider / annuler ----------

  it('cannot confirm while no game is selected', async () => {
    await createPopup();

    component.confirm();

    expect((element('popup-confirm') as HTMLButtonElement).disabled).toBeTrue();
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('confirm closes the popup with the selected games and the trimmed list name', async () => {
    await createPopup();
    const zelda = aSearchResult(1, 'Zelda');
    component.listNameInput = '  Loved  ';
    component.onAddGame(zelda);

    component.confirm();

    expect(dialogRef.close).toHaveBeenCalledOnceWith({addedGames: [zelda], listName: 'Loved'});
  });

  it('the confirm button says what will happen', async () => {
    await createPopup();
    expect(component.confirmLabel).toBe('Create list');

    component.onAddGame(aSearchResult(1, 'Zelda'));
    expect(component.confirmLabel).toBe('Create list with 1 game');

    component.onAddGame(aSearchResult(2, 'Mario'));
    expect(component.confirmLabel).toBe('Create list with 2 games');
  });

  it('the cross cancels: it closes without returning the selection', async () => {
    await createPopup();

    element('popup-close')!.click();

    expect(dialogRef.close).toHaveBeenCalledOnceWith();
  });

  it('cancelling with a selection asks before discarding it', async () => {
    await createPopup();
    component.onAddGame(aSearchResult(1, 'Zelda'));
    dialog.open.and.returnValue({afterClosed: () => of(false)} as MatDialogRef<unknown>);

    component.cancel();

    expect(dialog.open).toHaveBeenCalled();
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('cancelling with a selection closes once the user confirms', async () => {
    await createPopup();
    component.onAddGame(aSearchResult(1, 'Zelda'));
    dialog.open.and.returnValue({afterClosed: () => of(true)} as MatDialogRef<unknown>);

    component.cancel();

    expect(dialogRef.close).toHaveBeenCalledOnceWith();
  });
});
