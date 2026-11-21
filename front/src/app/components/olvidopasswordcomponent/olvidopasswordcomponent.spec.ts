import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Olvidopasswordcomponent } from './olvidopasswordcomponent';

describe('Olvidopasswordcomponent', () => {
  let component: Olvidopasswordcomponent;
  let fixture: ComponentFixture<Olvidopasswordcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Olvidopasswordcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Olvidopasswordcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
