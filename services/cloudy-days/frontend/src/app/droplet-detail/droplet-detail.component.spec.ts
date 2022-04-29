import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DropletDetailComponent } from './droplet-detail.component';

describe('DropletDetailComponent', () => {
  let component: DropletDetailComponent;
  let fixture: ComponentFixture<DropletDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DropletDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DropletDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
