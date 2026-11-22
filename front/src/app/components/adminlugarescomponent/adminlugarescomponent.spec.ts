import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AppModule } from '../../app-module';
import { Adminlugarescomponent } from './adminlugarescomponent';

describe('Adminlugarescomponent', () => {
  let component: Adminlugarescomponent;
  let fixture: ComponentFixture<Adminlugarescomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
      providers: [provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Adminlugarescomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
