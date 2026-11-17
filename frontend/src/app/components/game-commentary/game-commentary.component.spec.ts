import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {GameCommentaryComponent} from './game-commentary.component';
import {aGamesList, commonTestProviders} from '../../testing/test-helpers';

describe('GameCommentaryComponent', () => {
  let component: GameCommentaryComponent;
  let fixture: ComponentFixture<GameCommentaryComponent>;
  let http: HttpTestingController;
  let emitted: (string | null)[];

  const URL = '/api/game/gamesList/7/games/42/commentary';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GameCommentaryComponent],
      providers: commonTestProviders,
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(GameCommentaryComponent);
    component = fixture.componentInstance;
    component.gamesListId = 7;
    component.gameId = 42;
    component.gameName = 'Hades';
    emitted = [];
    component.commentaryChange.subscribe(value => emitted.push(value));
  });

  afterEach(() => http.verify());

  function render(commentary: string | null = null) {
    component.commentary = commentary;
    fixture.detectChanges();
  }

  function element(testId: string): HTMLElement | null {
    return (fixture.nativeElement as HTMLElement).querySelector(`[data-testid="${testId}"]`);
  }

  it('offers to add a comment when there is none', () => {
    render();

    expect(element('commentary-add-42')).not.toBeNull();
    expect(element('commentary-text-42')).toBeNull();
  });

  it('shows the existing comment', () => {
    render('Loved it');

    expect(element('commentary-text-42')?.textContent).toBe('Loved it');
    expect(element('commentary-add-42')).toBeNull();
  });

  it('saves a new comment, trimmed, and tells its parent', () => {
    render();
    component.startEditing();
    component.draft = '  Loved it  ';

    component.save();

    const request = http.expectOne(URL);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({text: 'Loved it'});
    request.flush({gamesListId: 7, gameId: 42, text: 'Loved it'});
    fixture.detectChanges();
    expect(emitted).toEqual(['Loved it']);
    expect(component.editing).toBeFalse();
    expect(element('commentary-text-42')?.textContent).toBe('Loved it');
  });

  it('starts editing with the current text', () => {
    render('Loved it');

    component.startEditing();

    expect(component.draft).toBe('Loved it');
  });

  it('does not send an empty comment', () => {
    render();
    component.startEditing();
    component.draft = '   ';

    component.save();

    http.expectNone(URL);
    expect(component.busy).toBeFalse();
  });

  it('keeps the editor open and explains when saving fails', () => {
    render();
    component.startEditing();
    component.draft = 'Loved it';

    component.save();
    http.expectOne(URL).flush('error', {status: 500, statusText: 'Server Error'});
    fixture.detectChanges();

    expect(component.editing).toBeTrue();
    expect(element('commentary-message-42')?.textContent).toContain('Could not save');
    expect(emitted).toEqual([]);
  });

  it('deletes the comment and tells its parent', () => {
    render('Loved it');

    component.delete();

    const request = http.expectOne(URL);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
    fixture.detectChanges();
    expect(emitted).toEqual([null]);
    expect(element('commentary-add-42')).not.toBeNull();
  });

  it('proposes the other lists as copy targets, not the current one', () => {
    render('Loved it');

    component.toggleCopyTargets();
    http.expectOne('/api/game/getGamesLists').flush([aGamesList(7, 'Current'), aGamesList(8, 'Wishlist')]);

    expect(component.copyTargets?.map(list => list.name)).toEqual(['Wishlist']);
  });

  it('copies the comment to another list', () => {
    render('Loved it');

    component.copyTo(aGamesList(8, 'Wishlist'));

    const request = http.expectOne(URL + '/transfer');
    expect(request.request.body).toEqual({targetListId: 8, replace: false});
    request.flush({gamesListId: 8, gameId: 42, text: 'Loved it'});
    expect(component.message).toBe('Comment copied to "Wishlist".');
  });

  it('asks before replacing a comment that already exists in the target list', () => {
    render('Loved it');
    const ask = spyOn(component, 'askConfirmation').and.returnValue(true);

    component.copyTo(aGamesList(8, 'Wishlist'));
    http.expectOne(URL + '/transfer').flush('conflict', {status: 409, statusText: 'Conflict'});

    expect(ask).toHaveBeenCalled();
    const retry = http.expectOne(URL + '/transfer');
    expect(retry.request.body).toEqual({targetListId: 8, replace: true});
    retry.flush({gamesListId: 8, gameId: 42, text: 'Loved it'});
  });

  it('does not replace the existing comment when the user refuses', () => {
    render('Loved it');
    spyOn(component, 'askConfirmation').and.returnValue(false);

    component.copyTo(aGamesList(8, 'Wishlist'));
    http.expectOne(URL + '/transfer').flush('conflict', {status: 409, statusText: 'Conflict'});

    http.expectNone(URL + '/transfer');
    expect(component.message).toBe('');
  });
});
