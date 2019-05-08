import { observable, action, flow } from "mobx";
import { Car, MAKERS } from "../model/Car";
import { carService } from "../services/CarService";
import { observer } from "mobx-react";

export class CarStore {
    @observable cars: Car[] = [];
    @observable detail: Car = {
      make: null,
      model: "",
      maturityDate: ""
    };
    @observable state = "pending";
    @observable dropDownOpen: boolean = false;
    
    @action
    updateMaturityDate(value: string) {
      this.detail.maturityDate = value;
    }
    @action
    updateDropDown(state: boolean) {
      this.dropDownOpen = state;
    }
  
    @action
    updateDropDownAndClose(make: string) {
      this.detail = { ...this.detail, make:MAKERS[make] };
      this.dropDownOpen = false;
    }
  
    @action
    updateDetailMake(value: string) {
      this.detail.make = MAKERS[value];
    }
  
    saveCar() {
      console.log(JSON.stringify(this.detail));
    }
  
    fetchCars = flow(function* () {
      this.state = "pending";
      try {
        const cars = yield carService.fetchCars();
        this.state = "done";
        this.cars = cars;
      } catch (error) {
        this.state = "error";
        console.error(error);
      }
    });
  }
  
  
