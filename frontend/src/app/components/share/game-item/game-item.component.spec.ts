import {ComponentFixture, TestBed} from '@angular/core/testing';
import {GameItemComponent} from './game-item.component';
import {aSearchResult} from '../../../testing/test-helpers';
import {GameSearchResult} from '../../../models/game-search.models';

describe('GameItemComponent', () => {
  let component: GameItemComponent;
  let fixture: ComponentFixture<GameItemComponent>;
  const zelda = aSearchResult(1, 'Zelda');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GameItemComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GameItemComponent);
    component = fixture.componentInstance;
    component.game = zelda;
    fixture.detectChanges();
  });

  function button(): HTMLButtonElement {
    return (fixture.nativeElement as HTMLElement).querySelector('[data-testid="game-item-add"]') as HTMLButtonElement;
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the name and the release year of the game', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.game-name')?.textContent?.trim()).toBe('Zelda');
    expect(element.querySelector('.game-year')?.textContent?.trim()).toBe('2020');
  });

  it('emits addGame when the game is not selected yet', () => {
    let added: GameSearchResult | undefined;
    component.addGame.subscribe(game => added = game);

    button().click();

    expect(added).toEqual(zelda);
  });

  it('emits removeGame when the game is already selected', () => {
    let removed: GameSearchResult | undefined;
    component.removeGame.subscribe(game => removed = game);
    component.gamesSelected = [zelda];

    button().click();

    expect(removed).toEqual(zelda);
  });

  it('cannot add a game that is already in the list', () => {
    let emitted = false;
    component.addGame.subscribe(() => emitted = true);
    component.alreadyInList = true;
    fixture.detectChanges();

    component.onAddGame();

    expect(button().disabled).toBeTrue();
    expect(button().getAttribute('aria-label')).toBe('Zelda is already in this list');
    expect(emitted).toBeFalse();
  });

  it('gives the button a name a screen reader can read', () => {
    expect(button().getAttribute('aria-label')).toBe('Add Zelda');
  });
});
