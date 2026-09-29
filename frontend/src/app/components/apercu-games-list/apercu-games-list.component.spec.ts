import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApercuGamesListComponent } from './apercu-games-list.component';

describe('ApercuListComponent', () => {
  let component: ApercuGamesListComponent;
  let fixture: ComponentFixture<ApercuGamesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApercuGamesListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApercuGamesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
