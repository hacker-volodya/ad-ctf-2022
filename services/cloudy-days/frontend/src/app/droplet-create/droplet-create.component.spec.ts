import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DropletCreateComponent } from './droplet-create.component';

describe('DropletCreateComponent', () => {
  let component: DropletCreateComponent;
  let fixture: ComponentFixture<DropletCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DropletCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DropletCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
