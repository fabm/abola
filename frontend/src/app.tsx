import * as mobx from "mobx";
import { flow, observable, action } from "mobx";
import * as React from "react";
import { observer } from "mobx-react";
import classNames from "classnames/bind";
import { Car, MAKERS } from "./model/Car";
import { carService } from "./services/CarService";
import { userService } from "./services/UserService";
import { CarStore } from "./stores/CarStore";
import { DropDownInput } from "./components/general/DropDownInput";

mobx.configure({ enforceActions: "observed" }); // don't allow state modifications outside actions

const readableMaker = (maker: MAKERS): string => MAKERS[maker]

const MakeComponent: React.FunctionComponent<{ store: CarStore }> = (props) => {
  return <h1>Make, {readableMaker(props.store.detail.make)}</h1>;
}

const DropDownButton: React.FunctionComponent<{ store: CarStore }> = (props) => <button
  className="btn btn-outline-secondary dropdown-toggle"
  type="button"
  data-toggle="dropdown"
  aria-haspopup="true"
  aria-expanded="false"
  onClick={() => {
    props.store.updateDropDown(!props.store.dropDownOpen);
  }}
  onBlur={(event) => {
    props.store.updateDropDown(false);
  }}
>Choose maker...</button>

@observer
class CarView extends React.Component<{ car: Car }, any> {
  render() {
    const car = this.props.car;
    return <div>{car.make}</div>;
  }
}


@observer
class CarEditor extends React.Component<{ store: CarStore }, any> {
  render() {
    const store = this.props.store;
    var classes = classNames(
      { "dropdown-menu": true },
      { show: store.dropDownOpen }
    );
    let labelsCount: number = Object.keys(MAKERS).length / 2;
    let makerLabels: string[] = [...Array(labelsCount).keys()].map(
      key => MAKERS[key]
    );

    return (
      <div className="col-12">
        <div className="form-group row">
          <label htmlFor="example-datetime-local-input" className="col-2 col-form-label">Maturity date</label>
        </div>
        <div className="col-6">
          <input className="form-control" type="datetime-local" id="example-datetime-local-input" onChange={(event) => {
            store.updateMaturityDate(event.target.value);
          }} />
        </div>
        <div className="form-group row">
          <label htmlFor="example-datetime-local-input" className="col-2 col-form-label">Model</label>
        </div>
        <div className="form-group row">
          <label htmlFor="example-datetime-local-input" className="col-2 col-form-label">Maker</label>
        </div>

        <DropDownInput
          current={store.detail.make}
          element={MAKERS}
          updateValue={(make) => store.updateDetailMake(make)}
          isOpen={store.dropDownOpen}
          updateState={(state) => store.updateDropDown(state)}
        />
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => store.saveCar()}
        >
          save car
        </button>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => userService.userLogout()}
        >logout</button>
      </div>
    );
  }
}

@observer
class CarsList extends React.Component<{ stores: { carStore: CarStore } }, any> {
  render() {
    const store = this.props.stores.carStore;
    return (
      <div>
        <ul>
          {store.cars.map((car, idx) => (
            <CarView key={idx} car={car} />
          ))}
        </ul>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => store.fetchCars()}
        >
          fetch cars
        </button>
      </div>
    );
  }
}
const carStore = new CarStore();

export const App = () => {
  return (
    <div>
      <CarsList stores={{ carStore: carStore }} />
      <CarEditor store={carStore} />
    </div>
  );
};

// To test api
window["test.api"] = {
  registerUser: userService.registerUser,
  login: userService.userLogin,
  createCar: carService.createCar
};

userService.registerUser({ username: "xico", email: "xico@guarda.pt", password: "xpto" });
userService.userLogin({ username: "xico", password: "xpto" });

setTimeout(() => {
  window["test.api"]
    .createCar({
      make: "VOLKSWAGEN",
      model: "golf 5",
      maturityDate: "2019-05-05T20:13:42",
      price: 280000
    })
    .then(x => { });
}, 3000);