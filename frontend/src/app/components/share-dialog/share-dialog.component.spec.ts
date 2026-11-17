import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpTestingController} from '@angular/common/http/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ShareDialogComponent} from './share-dialog.component';
import {aGamesList, commonTestProviders} from '../../testing/test-helpers';
import {ShareLink} from '../../models/share.model';

describe('ShareDialogComponent', () => {
  let component: ShareDialogComponent;
  let fixture: ComponentFixture<ShareDialogComponent>;
  let http: HttpTestingController;

  const walletLink: ShareLink = {id: 1, token: 'tok-wallet', scope: 'WALLET', includeCommentaries: false, listNames: [], createdAt: '2026-01-01T10:00:00'};

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [ShareDialogComponent],
      providers: [
        ...commonTestProviders,
        {provide: MAT_DIALOG_DATA, useValue: {gamesLists: [aGamesList(7, 'Loved'), aGamesList(8, 'Wishlist')]}},
        {provide: MatDialogRef, useValue: {close: () => {}}},
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ShareDialogComponent);
    component = fixture.componentInstance;
  });

  /** Répond à la requête de chargement des liens faite à l'ouverture. */
  function loadLinks(links: ShareLink[] = []) {
    http.expectOne(request => request.url === '/api/share' && request.method === 'GET').flush(links);
    fixture.detectChanges();
  }

  function element(testId: string): HTMLElement | null {
    return (fixture.nativeElement as HTMLElement).querySelector(`[data-testid="${testId}"]`);
  }

  it('lists the existing links with their full address', () => {
    loadLinks([walletLink]);

    expect(element('share-link-1')?.textContent).toContain('Whole wallet');
    expect((element('share-link-url') as HTMLInputElement).value).toBe(`${location.origin}/share/tok-wallet`);
  });

  it('says when nothing is shared yet', () => {
    loadLinks();

    expect(element('share-no-link')).not.toBeNull();
  });

  it('creates a link for the whole wallet by default', () => {
    loadLinks();

    component.create();

    const request = http.expectOne(r => r.url === '/api/share' && r.method === 'POST');
    expect(request.request.body).toEqual({scope: 'WALLET', listIds: undefined, includeCommentaries: false});
    request.flush(walletLink);
    expect(component.links).toEqual([walletLink]);
  });

  it('creates a link for the chosen lists, with comments when asked', () => {
    loadLinks();
    component.scope = 'LISTS';
    component.toggleList(8);
    component.includeCommentaries = true;

    component.create();

    const request = http.expectOne(r => r.url === '/api/share' && r.method === 'POST');
    expect(request.request.body).toEqual({scope: 'LISTS', listIds: [8], includeCommentaries: true});
    request.flush({...walletLink, id: 2, scope: 'LISTS', listNames: ['Wishlist'], includeCommentaries: true});
    expect(component.describe(component.links[0])).toBe('Wishlist · with comments');
  });

  it('cannot create a "chosen lists" link without choosing a list', () => {
    loadLinks();
    component.scope = 'LISTS';

    component.create();

    expect(component.canCreate).toBeFalse();
    http.expectNone(r => r.url === '/api/share' && r.method === 'POST');
  });

  it('checking a list twice unselects it', () => {
    component.toggleList(7);
    component.toggleList(7);

    expect(component.selectedListIds.size).toBe(0);
  });

  it('revoking a link removes it from the list', () => {
    loadLinks([walletLink]);

    component.revoke(walletLink);

    const request = http.expectOne('/api/share/1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
    expect(component.links).toEqual([]);
  });

  it('keeps the link and explains when revoking fails', () => {
    loadLinks([walletLink]);

    component.revoke(walletLink);
    http.expectOne('/api/share/1').flush('error', {status: 500, statusText: 'Server Error'});
    fixture.detectChanges();

    expect(component.links).toEqual([walletLink]);
    expect(element('share-message')?.textContent).toContain('Could not revoke');
  });
});
