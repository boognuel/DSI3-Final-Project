import { RolModel } from './rol.model';

export interface SesionModel {
  token: string;
  role: RolModel;
  username: string;
}
