import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthDialogComponent } from './auth-dialog.component';
import {MatDialogRef} from '@angular/material/dialog';
import {commonTestProviders} from '../../testing/test-helpers';

describe('AuthDialogComponent', () => {
  let component: AuthDialogComponent;
  let fixture: ComponentFixture<AuthDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthDialogComponent],
      providers: [...commonTestProviders, {provide: MatDialogRef, useValue: {close: () => {}}}],
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AuthDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
