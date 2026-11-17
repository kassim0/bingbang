import {TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {WalletService} from './wallet.service';
import {AuthService} from './auth.service';
import {commonTestProviders} from '../testing/test-helpers';

describe('WalletService', () => {
  let service: WalletService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({providers: commonTestProviders});
    http = TestBed.inject(HttpTestingController);
    service = TestBed.inject(WalletService);
  });

  afterEach(() => document.body.removeAttribute('data-bb-background'));

  function connect() {
    TestBed.inject(AuthService).connectedUser.set({email: 'a@test.com', username: 'alex', guest: false});
    TestBed.flushEffects();
  }

  it('starts with the default background', () => {
    TestBed.flushEffects();

    expect(service.background()).toBe('COSMIC');
    expect(document.body.getAttribute('data-bb-background')).toBe('COSMIC');
  });

  it('loads the background of the connected user and applies it to the page', () => {
    connect();
    http.expectOne('/api/wallet').flush({background: 'AURORA'});
    TestBed.flushEffects();

    expect(service.background()).toBe('AURORA');
    expect(document.body.getAttribute('data-bb-background')).toBe('AURORA');
  });

  it('applies a new background immediately and saves it', () => {
    TestBed.flushEffects(); // état initial appliqué, comme au démarrage de l'application
    service.setBackground('SUNSET');
    TestBed.flushEffects();

    expect(document.body.getAttribute('data-bb-background')).toBe('SUNSET');
    const request = http.expectOne('/api/wallet/background');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({background: 'SUNSET'});
    request.flush({background: 'SUNSET'});
    expect(service.background()).toBe('SUNSET');
  });

  it('goes back to the previous background when saving fails', () => {
    service.setBackground('SUNSET');
    http.expectOne('/api/wallet/background').flush('error', {status: 500, statusText: 'Server Error'});

    expect(service.background()).toBe('COSMIC');
  });

  it('goes back to the default background after a logout', () => {
    connect();
    http.expectOne('/api/wallet').flush({background: 'AURORA'});

    TestBed.inject(AuthService).connectedUser.set(null);
    TestBed.flushEffects();

    expect(service.background()).toBe('COSMIC');
  });
});
