import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {GamesSearchPopupComponent} from './games-search-popup.component';
import {aSearchResult, commonTestProviders} from '../../testing/test-helpers';

describe('ListGamesPopupComponent', () => {
  let component: GamesSearchPopupComponent;
  let fixture: ComponentFixture<GamesSearchPopupComponent>;
  let http: HttpTestingController;
  let dialogRef: jasmine.SpyObj<MatDialogRef<GamesSearchPopupComponent>>;

  beforeEach(async () => {
    dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    await TestBed.configureTestingModule({
      imports: [GamesSearchPopupComponent],
      providers: [
        ...commonTestProviders,
        {provide: MAT_DIALOG_DATA, useValue: {}},
        {provide: MatDialogRef, useValue: dialogRef},
      ],
    })
    .compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(GamesSearchPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the games returned by the search', () => {
    component.receiveData('zelda');
    http.expectOne(request => request.url === '/api/games/search' && request.params.get('name') === 'zelda')
      .flush([aSearchResult(1, 'Zelda BOTW'), aSearchResult(2, 'Zelda TOTK')]);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('Zelda BOTW');
    expect(text).toContain('Zelda TOTK');
    expect(component.isSearching).toBeFalse();
  });

  it('stops the spinner when the search fails', () => {
    component.receiveData('zelda');
    http.expectOne(request => request.url === '/api/games/search').flush('error', {status: 502, statusText: 'Bad Gateway'});

    expect(component.isSearching).toBeFalse();
  });

  it('adds and removes a game from the selection', () => {
    const zelda = aSearchResult(1, 'Zelda');
    const mario = aSearchResult(2, 'Mario');

    component.onAddGame(zelda);
    component.onAddGame(mario);
    component.onRemoveGame(zelda);

    expect(component.addedGames).toEqual([mario]);
  });

  // ---------- règle « au moins un nom ou des jeux » ----------

  it('returns a list with only a name', () => {
    component.listNameInput = '  Wishlist ';

    component.close();

    expect(dialogRef.close).toHaveBeenCalledOnceWith({addedGames: [], listName: 'Wishlist'});
  });

  it('returns a list with only games', () => {
    const zelda = aSearchResult(1, 'Zelda');
    component.onAddGame(zelda);

    component.close();

    expect(dialogRef.close).toHaveBeenCalledOnceWith({addedGames: [zelda], listName: ''});
  });

  it('returns nothing when there is neither a name nor a game', () => {
    component.listNameInput = '   ';

    component.close();

    expect(dialogRef.close).toHaveBeenCalledOnceWith();
  });
});
