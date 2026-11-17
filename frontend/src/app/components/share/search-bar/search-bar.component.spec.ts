import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchBarComponent } from './search-bar.component';
import {commonTestProviders} from '../../../testing/test-helpers';

describe('SearchBarComponent', () => {
  let component: SearchBarComponent;
  let fixture: ComponentFixture<SearchBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchBarComponent],
      providers: commonTestProviders,
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SearchBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('emits the trimmed text to search', () => {
    let searched: string | undefined;
    component.searchSubmitted.subscribe(text => searched = text);
    component.inputResult = '  zelda ';

    component.sendInput();

    expect(searched).toBe('zelda');
  });

  it('does not search an empty text', () => {
    let emitted = false;
    component.searchSubmitted.subscribe(() => emitted = true);
    component.inputResult = '   ';

    component.sendInput();

    expect(emitted).toBeFalse();
  });

  it('has a real search button that a keyboard can reach', () => {
    const button = (fixture.nativeElement as HTMLElement).querySelector('[data-testid="search-bar-submit"]');

    expect(button?.tagName).toBe('BUTTON');
    expect(button?.getAttribute('aria-label')).toBe('Search');
  });
});
