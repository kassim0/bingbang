import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ApercuGamesListComponent} from './apercu-games-list.component';
import {aGamesList} from '../../testing/test-helpers';

describe('ApercuListComponent', () => {
  let component: ApercuGamesListComponent;
  let fixture: ComponentFixture<ApercuGamesListComponent>;

  function render(gameNames: string[]): HTMLElement {
    component.gamesList = aGamesList(1, 'My list', gameNames);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApercuGamesListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApercuGamesListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    render([]);
    expect(component).toBeTruthy();
  });

  it('shows the name of the list', () => {
    expect(render(['Hades']).textContent).toContain('My list');
  });

  it('shows only the first 3 games', () => {
    const element = render(['Hades', 'Celeste', 'Portal 2', 'Outer Wilds']);

    expect(element.querySelectorAll('.item-apercu-list').length).toBe(3);
    expect(element.textContent).toContain('Portal 2');
    expect(element.textContent).not.toContain('Outer Wilds');
  });

  it('says when the list is empty', () => {
    const element = render([]);

    expect(element.querySelectorAll('.item-apercu-list').length).toBe(0);
    expect(element.textContent?.toLowerCase()).toContain('empty');
  });
});
