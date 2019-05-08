import * as mobx from "mobx";
import { flow, observable, action } from "mobx";
import * as React from "react";
import { observer } from "mobx-react";
import classNames from "classnames/bind";
import { ObservableArray } from "mobx/lib/internal";
import axios from "axios";

mobx.configure({ enforceActions: "observed" }); // don't allow state modifications outside actions

interface User {
  username: string;
  password: string;
  email?: string;
}

let registerUser = (user: User): Promise<any> => {
  let body = {
    name: user.username,
    email: user.email,
    password: user.password
  };
  return fetch("api/user", { method: "POST", body: JSON.stringify(body) }).then(
    res => res.text()
  );
};

let userLogin = (user: User): Promise<any> => {
  let body = {
    user: user.username,
    pass: user.password
  };
  return fetch("api/user/login", {
    method: "POST",
    body: JSON.stringify(body)
  }).then(res => res.text());
};

let userLogout = (): Promise<any> => {
  return axios.get("/api/user/logout");
};

class UserStore {
  @observable user;
}

enum MAKERS {
  VOLKSVAGEN,
  RENAULT
}

interface Car {
  make: string;
  model?: string;
  maturityDate?: string;
  price?: number;
}

let createCar = (car: Car): Promise<any> => {
  return axios
    .post("api/car", car, { withCredentials: true })
    .then(res => res.data);
};

let getCar = (car: Car): Promise<any> => {
  return fetch("api/car", { method: "POST", body: JSON.stringify(car) }).then(
    res => res.json()
  );
};

let fetchCars = (): Promise<Car[]> => {
  return fetch("api/car/list").then(res => res.json());
};


class CarStore {
  @observable cars: Car[] = [];
  @observable detail: Car = {
    make: "",
    model: "",
    maturityDate: ""
  };
  @observable state = "pending";
  @observable dropDownOpen: boolean = false;

  @action
  updateDropDown(state: boolean) {
    this.dropDownOpen = state;
  }

  @action
  updateDropDownAndClose(make: string) {
    this.detail = { ...this.detail, make };
    this.dropDownOpen = false;
  }

  @action
  updateDetailMake(value: string) {
    this.detail.make = value;
  }

  saveCar() {
    console.log(JSON.stringify(this.detail));
  }

  fetchCars = flow(function* () {
    this.state = "pending";
    try {
      const cars = yield fetchCars();
      this.state = "done";
      this.cars = cars;
    } catch (error) {
      this.state = "error";
      console.error(error);
    }
  });
}

@observer
class CarView extends React.Component<{ car: Car }, any> {
  render() {
    const car = this.props.car;
    return <div>{car.make}</div>;
  }
}

let DropDownButton = (store: CarStore) => {
  return <button
    className="btn btn-outline-secondary dropdown-toggle"
    type="button"
    data-toggle="dropdown"
    aria-haspopup="true"
    aria-expanded="false"
    onClick={() => {
      store.updateDropDown(!store.dropDownOpen);
    }}
    onBlur={(event) => {
      store.updateDropDown(false);
    }}
  >
    Choose maker...
</button>

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
          <input className="form-control" type="datetime-local" value="2011-08-19T13:45:00" id="example-datetime-local-input" />
        </div>
        <div className="form-group row">
          <label htmlFor="example-datetime-local-input" className="col-2 col-form-label">Model</label>
        </div>
        <div className="col-6">
          <input
            type="text"
            onChange={cmp => store.updateDetailMake(cmp.target.value)}
          />
        </div>
        <div className="form-group row">
          <label htmlFor="example-datetime-local-input" className="col-2 col-form-label">Maker</label>
        </div>
        <div className="col-6">
          <div className="input-group mb-3">
            <div className="input-group-prepend">
              {DropDownButton(store)}
              <div className={classes}>
                {makerLabels.map(maker => {
                  return (
                    <a
                      key={maker}
                      onMouseDown={(event) => event.preventDefault()}
                      onClick={(event) => {
                        store.updateDropDownAndClose(maker);
                      }}
                      className="dropdown-item"
                      href="#"
                    >
                      {maker}
                    </a>
                  );
                })}
              </div>
            </div>
            <input
              type="text"
              className="form-control"
              aria-label="Text input with dropdown button"
              value={store.detail.make}
              disabled
            />
          </div>
        </div>
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
          onClick={() => userLogout()}
        >
          logout
        </button>
      </div>
    );
  }
}

@observer
class CarsList extends React.Component<{ store: CarStore }, any> {
  render() {
    const store = this.props.store;
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
      <CarsList store={carStore} />
      <CarEditor store={carStore} />
    </div>
  );
};

// To test api
window["test.api"] = {
  registerUser: registerUser,
  login: userLogin,
  createCar: createCar
};

registerUser({ username: "xico", email: "xico@guarda.pt", password: "xpto" });
userLogin({ username: "xico", password: "xpto" });

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
