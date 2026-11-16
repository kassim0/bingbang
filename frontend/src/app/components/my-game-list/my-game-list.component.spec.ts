import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {of} from 'rxjs';
import {MyGameListComponent} from './my-game-list.component';
import {aGamesList, aSearchResult, commonTestProviders} from '../../testing/test-helpers';

describe('MyGameListComponent', () => {
  let component: MyGameListComponent;
  let fixture: ComponentFixture<MyGameListComponent>;
  let http: HttpTestingController;
  let dialogRef: jasmine.SpyObj<MatDialogRef<MyGameListComponent>>;
  let dialog: jasmine.SpyObj<MatDialog>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const UPDATE_URL = '/api/game/updateGamesList';
  const LISTS_URL = '/api/game/getGamesLists';

  beforeEach(async () => {
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close', 'backdropClick', 'keydownEvents']);
    dialogRef.backdropClick.and.returnValue(of());
    dialogRef.keydownEvents.and.returnValue(of());
    dialog = jasmine.createSpyObj('MatDialog', ['open']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    await TestBed.configureTestingModule({
      imports: [MyGameListComponent],
      providers: [
        ...commonTestProviders,
        {provide: MAT_DIALOG_DATA, useValue: {gamesList: aGamesList(7, 'Loved', ['Hades', 'Celeste'])}},
        {provide: MatDialogRef, useValue: dialogRef},
        {provide: MatDialog, useValue: dialog},
        {provide: MatSnackBar, useValue: snackBar},
      ],
    })
    .compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(MyGameListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  /** Fait répondre la popup de confirmation : true = l'utilisateur confirme, false = il annule. */
  function userAnswers(confirmed: boolean) {
    dialog.open.and.returnValue({afterClosed: () => of(confirmed)} as MatDialogRef<unknown>);
  }

  function element(testId: string): HTMLButtonElement {
    return (fixture.nativeElement as HTMLElement).querySelector(`[data-testid="${testId}"]`) as HTMLButtonElement;
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the games of the list', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('Hades');
    expect(text).toContain('Celeste');
  });

  it('marks a game for removal, and unmarks it on a second click', () => {
    const hades = component.gamesList.games[0].game;

    component.onDeleteGame(hades);
    expect(component.isGameSelected(hades)).toBeTrue();

    component.onDeleteGame(hades);
    expect(component.isGameSelected(hades)).toBeFalse();
  });

  // ---------- Save ----------

  it('sends the new name and the games to remove when saving', () => {
    component.editedName = 'Favourites';
    component.onDeleteGame(component.gamesList.games[1].game);

    component.saveModifGamesList();

    const request = http.expectOne(UPDATE_URL);
    expect(request.request.body.gamesListId).toBe(7);
    expect(request.request.body.newName).toBe('Favourites');
    expect(request.request.body.removeGameId).toEqual([component.gamesList.games[1].game.id]);
    request.flush('Games list updated');
    http.expectOne(LISTS_URL).flush([]);
  });

  it('does not send a name that did not change', () => {
    component.onDeleteGame(component.gamesList.games[0].game);

    component.saveModifGamesList();

    const request = http.expectOne(UPDATE_URL);
    expect(request.request.body.newName).toBeNull();
    request.flush('Games list updated');
    http.expectOne(LISTS_URL).flush([]);
  });

  it('Save is disabled, and sends nothing, while there is nothing to save', () => {
    component.saveModifGamesList();

    expect(element('my-list-save').disabled).toBeTrue();
    http.expectNone(UPDATE_URL);
  });

  it('cannot save an empty name', () => {
    component.editedName = '   ';

    component.saveModifGamesList();

    expect(component.canSave).toBeFalse();
    http.expectNone(UPDATE_URL);
  });

  it('after a successful save: shows the updated list, clears the removals and tells the user', () => {
    component.editedName = 'Favourites';
    component.onDeleteGame(component.gamesList.games[1].game);

    component.saveModifGamesList();
    http.expectOne(UPDATE_URL).flush('Games list updated');
    http.expectOne(LISTS_URL).flush([aGamesList(7, 'Favourites', ['Hades'])]);
    fixture.detectChanges();

    expect(component.gamesList.name).toBe('Favourites');
    expect(component.gamesList.games.length).toBe(1);
    expect(component.selectedGames).toEqual([]);
    expect(component.hasChanges).toBeFalse();
    expect(snackBar.open).toHaveBeenCalledWith('List saved', undefined, jasmine.anything());
    expect((fixture.nativeElement as HTMLElement).textContent).not.toContain('Celeste');
  });

  it('tells the user when the save fails, and keeps the pending changes', () => {
    component.onDeleteGame(component.gamesList.games[0].game);

    component.saveModifGamesList();
    http.expectOne(UPDATE_URL).flush('error', {status: 500, statusText: 'Server Error'});

    expect(snackBar.open).toHaveBeenCalledWith('Could not save the list. Please try again.', undefined, jasmine.anything());
    expect(component.selectedGames.length).toBe(1);
    expect(component.saving).toBeFalse();
  });

  it('adding games does not erase a name that is being edited', () => {
    component.editedName = 'Not saved yet';
    dialog.open.and.returnValue({afterClosed: () => of({addedGames: [aSearchResult(1, 'Zelda')], listName: ''})} as MatDialogRef<unknown>);

    component.openGamesSearchPopup();
    http.expectOne(UPDATE_URL).flush('Games list updated');
    http.expectOne(LISTS_URL).flush([aGamesList(7, 'Loved', ['Hades', 'Celeste', 'Zelda'])]);

    expect(component.gamesList.games.length).toBe(3);
    expect(component.editedName).toBe('Not saved yet');
    expect(component.hasChanges).toBeTrue();
  });

  // ---------- Delete ----------

  it('asks for confirmation before deleting, and deletes nothing if the user cancels', () => {
    userAnswers(false);

    component.deleteGamesList();

    expect(dialog.open).toHaveBeenCalled();
    http.expectNone('/api/game/deleteGamesList/7');
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('deletes the list and closes once the user confirms', () => {
    userAnswers(true);

    component.deleteGamesList();
    http.expectOne('/api/game/deleteGamesList/7').flush('GamesList deleted');

    expect(dialogRef.close).toHaveBeenCalled();
  });

  // ---------- Fermeture ----------

  it('closes directly when nothing changed', () => {
    component.close();

    expect(dialog.open).not.toHaveBeenCalled();
    expect(dialogRef.close).toHaveBeenCalled();
  });

  it('asks before closing with unsaved changes', () => {
    userAnswers(false);
    component.editedName = 'Other name';

    component.close();

    expect(dialog.open).toHaveBeenCalled();
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('gives each remove button a name a screen reader can read', () => {
    const hadesId = component.gamesList.games[0].game.id;

    expect(element('delete-game-' + hadesId).getAttribute('aria-label')).toBe('Remove Hades');
  });
});
