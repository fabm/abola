export enum MAKERS {
  VOLKSWAGEN,
  RENAULT,
  AUDI
}

export interface Car {
  make: MAKERS;
  model?: string;
  maturityDate?: string;
  price?: number;
}

export function carToJson(car:Car){
  let json = (car as any)
  json.make = MAKERS[car.make];
  return json;
}