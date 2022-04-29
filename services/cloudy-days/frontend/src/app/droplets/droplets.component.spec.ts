import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DropletsComponent } from './droplets.component';

describe('DropletsComponent', () => {
  let component: DropletsComponent;
  let fixture: ComponentFixture<DropletsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DropletsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DropletsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
