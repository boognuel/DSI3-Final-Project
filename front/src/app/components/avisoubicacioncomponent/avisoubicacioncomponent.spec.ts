import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Avisoubicacioncomponent } from './avisoubicacioncomponent';

describe('Avisoubicacioncomponent', () => {
  let component: Avisoubicacioncomponent;
  let fixture: ComponentFixture<Avisoubicacioncomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Avisoubicacioncomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Avisoubicacioncomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
