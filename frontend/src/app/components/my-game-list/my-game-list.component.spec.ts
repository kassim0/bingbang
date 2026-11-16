import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MyGameListComponent} from './my-game-list.component';
import {aGamesList, commonTestProviders} from '../../testing/test-helpers';

describe('MyGameListComponent', () => {
  let component: MyGameListComponent;
  let fixture: ComponentFixture<MyGameListComponent>;
  let http: HttpTestingController;
  let dialogRef: jasmine.SpyObj<MatDialogRef<MyGameListComponent>>;

  beforeEach(async () => {
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    await TestBed.configureTestingModule({
      imports: [MyGameListComponent],
      providers: [
        ...commonTestProviders,
        {provide: MAT_DIALOG_DATA, useValue: {gamesList: aGamesList(7, 'Loved', ['Hades', 'Celeste'])}},
        {provide: MatDialogRef, useValue: dialogRef},
      ],
    })
    .compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(MyGameListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

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

  it('sends the new name and the games to remove when saving', () => {
    component.editedName = 'Favourites';
    component.onDeleteGame(component.gamesList.games[1].game);

    component.saveModifGamesList();

    const request = http.expectOne('/api/game/updateGamesList');
    expect(request.request.body.gamesListId).toBe(7);
    expect(request.request.body.newName).toBe('Favourites');
    expect(request.request.body.removeGameId).toEqual([component.gamesList.games[1].game.id]);
    request.flush('Games list updated');
  });

  it('does not send a name that did not change', () => {
    component.saveModifGamesList();

    const request = http.expectOne('/api/game/updateGamesList');
    expect(request.request.body.newName).toBeNull();
    request.flush('Nothing to update');
  });
});
