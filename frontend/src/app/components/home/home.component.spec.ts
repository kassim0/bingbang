import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {HomeComponent} from './home.component';
import {AuthService} from '../../services/auth.service';
import {aGamesList, commonTestProviders} from '../../testing/test-helpers';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let http: HttpTestingController;

  const LISTS_URL = '/api/game/getGamesLists';

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: commonTestProviders,
    })
    .compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  /** Simule un utilisateur connecté : la home charge alors ses listes. */
  function connect() {
    TestBed.inject(AuthService).connectedUser.set({email: 'a@test.com', username: 'alex', guest: false});
    fixture.detectChanges();
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not load the lists while nobody is connected', () => {
    http.expectNone(LISTS_URL);
    expect((fixture.nativeElement as HTMLElement).querySelector('[data-testid="open-search-popup"]')).toBeNull();
  });

  it('shows one card per games list of the connected user', () => {
    connect();
    http.expectOne(LISTS_URL).flush([aGamesList(1, 'Loved', ['Hades']), aGamesList(2, 'To play')]);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[data-testid="games-list-card-1"]')?.textContent).toContain('Loved');
    expect(element.querySelector('[data-testid="games-list-card-2"]')?.textContent).toContain('To play');
    expect(element.querySelector('[data-testid="open-search-popup"]')).not.toBeNull();
    expect(element.querySelector('[data-testid="home-empty-state"]')).toBeNull();
  });

  it('welcomes a user who has no list yet', () => {
    connect();
    http.expectOne(LISTS_URL).flush([]);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('[data-testid="home-empty-state"]')).not.toBeNull();
  });

  it('does not say "no list" while the lists are still loading', () => {
    connect();

    expect((fixture.nativeElement as HTMLElement).querySelector('[data-testid="home-empty-state"]')).toBeNull();
    http.expectOne(LISTS_URL).flush([]);
  });

  it('tells the user when the lists cannot be loaded, and can try again', () => {
    connect();
    http.expectOne(LISTS_URL).flush('error', {status: 500, statusText: 'Server Error'});
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('[data-testid="home-load-error"]')).not.toBeNull();

    component.reload();
    http.expectOne(LISTS_URL).flush([aGamesList(1, 'Loved')]);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('[data-testid="home-load-error"]')).toBeNull();
    expect((fixture.nativeElement as HTMLElement).querySelector('[data-testid="games-list-card-1"]')).not.toBeNull();
  });

  it('only keeps the latest answer when two reloads overlap', () => {
    connect();
    const first = http.expectOne(LISTS_URL);

    component.reload();

    expect(first.cancelled).toBeTrue();
    http.expectOne(LISTS_URL).flush([aGamesList(2, 'Latest')]);
    expect(component.gamesLists.map(list => list.name)).toEqual(['Latest']);
  });
});
