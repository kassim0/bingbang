import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {ActivatedRoute, convertToParamMap, provideRouter} from '@angular/router';
import {SharedViewComponent} from './shared-view.component';
import {AuthService} from '../../services/auth.service';
import {WalletService} from '../../services/wallet.service';
import {aGamesList, commonTestProviders} from '../../testing/test-helpers';
import {SharedWallet} from '../../models/share.model';

describe('SharedViewComponent', () => {
  let fixture: ComponentFixture<SharedViewComponent>;
  let http: HttpTestingController;

  const VIEW_URL = '/api/share/view/tok-123';

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [SharedViewComponent],
      providers: [
        ...commonTestProviders,
        provideRouter([]),
        {provide: ActivatedRoute, useValue: {snapshot: {paramMap: convertToParamMap({token: 'tok-123'})}}},
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(SharedViewComponent);
    fixture.detectChanges();
  });

  afterEach(() => document.body.removeAttribute('data-bb-background'));

  /** Simule la fin de la connexion du visiteur (invité créé par AuthService). */
  function visitorIsConnected() {
    TestBed.inject(AuthService).connectedUser.set({email: 'guest@bingbang.invalid', username: 'guest', guest: true});
    fixture.detectChanges();
  }

  function sharedWallet(overrides: Partial<SharedWallet> = {}): SharedWallet {
    const loved = aGamesList(7, 'Loved', ['Hades']);
    loved.games[0].commentary = 'One more run';
    return {ownerName: 'alex', scope: 'WALLET', background: 'OCEAN', lists: [loved, aGamesList(8, 'Wishlist')], ...overrides};
  }

  function element(testId: string): HTMLElement | null {
    return (fixture.nativeElement as HTMLElement).querySelector(`[data-testid="${testId}"]`);
  }

  it('waits for the visitor to be connected before asking the server', () => {
    http.expectNone(VIEW_URL);

    visitorIsConnected();

    http.expectOne(VIEW_URL).flush(sharedWallet());
  });

  it('shows the owner, the lists, their games and the shared comments', () => {
    visitorIsConnected();
    http.expectOne(VIEW_URL).flush(sharedWallet());
    fixture.detectChanges();

    expect(element('shared-banner')?.textContent).toContain("alex's games wallet");
    expect(element('shared-banner')?.textContent).toContain('Read only');
    expect(element('shared-list-7')?.textContent).toContain('Hades');
    expect(element('shared-list-7')?.textContent).toContain('One more run');
    expect(element('shared-list-8')?.textContent).toContain('This list is empty.');
  });

  it('has no button at all: the page is read only', () => {
    visitorIsConnected();
    http.expectOne(VIEW_URL).flush(sharedWallet());
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelectorAll('button, input, textarea').length).toBe(0);
  });

  it('shows the wallet with the background of its owner, and gives it back when leaving', () => {
    const walletService = TestBed.inject(WalletService);
    visitorIsConnected();
    http.expectOne(VIEW_URL).flush(sharedWallet());

    expect(walletService.previewBackground()).toBe('OCEAN');

    fixture.destroy();
    expect(walletService.previewBackground()).toBeNull();
  });

  it('explains when the link does not exist or was revoked', () => {
    visitorIsConnected();
    http.expectOne(VIEW_URL).flush('Not found', {status: 404, statusText: 'Not Found'});
    fixture.detectChanges();

    expect(element('shared-not-found')).not.toBeNull();
    expect(element('shared-banner')).toBeNull();
  });
});
