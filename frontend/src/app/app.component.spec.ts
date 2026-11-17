import {TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {AppComponent} from './app.component';
import {commonTestProviders} from './testing/test-helpers';

describe('AppComponent', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [...commonTestProviders, provideRouter([])],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have the 'frontend' title`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('frontend');
  });

  // Le test généré par Angular CLI attendait "Hello, frontend", un texte qui n'existe plus :
  // le titre affiché par l'application est le logo du navbar.
  it('should render title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('BINGBANG');
  });
});
