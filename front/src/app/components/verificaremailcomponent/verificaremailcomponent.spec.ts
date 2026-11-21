import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Verificaremailcomponent } from './verificaremailcomponent';

describe('Verificaremailcomponent', () => {
  let component: Verificaremailcomponent;
  let fixture: ComponentFixture<Verificaremailcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Verificaremailcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Verificaremailcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
