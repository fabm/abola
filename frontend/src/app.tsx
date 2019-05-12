import { observable, configure as configureMbox, computed, action } from "mobx";
import * as React from "react";
import { carService } from "./services/CarService";
import { CarsList, carStore } from "./components/app/CarList";
import { CarEditor } from "./components/app/CarEditor";
import {
  Notifications,
  notificationStore
} from "./components/app/Notifications";
import { LoginEditor } from "./components/app/LoginEditor";
import { observer } from "mobx-react";
import { UserRegisterEditor } from "./components/app/UserRegisterEditor";

configureMbox({ enforceActions: "observed" }); // don't allow state modifications outside actions

function getCookie(cname) {
  var name = cname + "=";
  var decodedCookie = decodeURIComponent(document.cookie);
  var ca = decodedCookie.split(";");
  for (var i = 0; i < ca.length; i++) {
    var c = ca[i];
    while (c.charAt(0) == " ") {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return "";
}

interface AppStateValues {
  register: boolean;
  userName: string;
}

enum AppState {
  LOGGED_IN,
  WAIT_LOG_IN,
  SHOWING_REGISTER
}

class AppStateStore {
  @observable
  appStateValues: AppStateValues = {
    register: false,
    userName: localStorage.getItem("username")
  };

  @computed
  get state(): AppState {
    console.log("try to calculate");
    if (this.appStateValues.userName === "" || this.appStateValues.userName === null || this.appStateValues.userName === undefined) {
      if (this.appStateValues.register) {
        return AppState.SHOWING_REGISTER;
      }
      return AppState.WAIT_LOG_IN;
    }
    return AppState.LOGGED_IN;
  }

  @action
  updateShowRegister(showRegister: boolean) {
    this.appStateValues.register = showRegister;
  }
  @action
  updateUserName(userName: string) {
    this.appStateValues.userName = userName;
  }
}

let appStateStore = new AppStateStore();

@observer
export class App extends React.Component<{}, {}> {
  render() {
    console.log("render " + appStateStore.state);
    return (
      <div className="container" style={{ marginBottom: "5rem" }}>
        <Notifications />
        {appStateStore.state === AppState.LOGGED_IN && (
          <div>Hello {appStateStore.appStateValues.userName}</div>
        )}
        <CarsList />
        {appStateStore.state === AppState.LOGGED_IN && (
          <CarEditor
            saveCarEvent={car => {
              carService.createCar(car).then(res => {
                if (res.status == 204) {
                  let notification = notificationStore.createNotification();
                  notification.content = <div>Successefuly created</div>;
                  notificationStore.addNotificationTemp(notification, 3000);
                  carStore.createCar(car);
                }
              });
            }}
            logoutEvent={() => {
              localStorage.removeItem(appStateStore.appStateValues.userName);
            }}
          />
        )}
        {appStateStore.state === AppState.WAIT_LOG_IN && (
          <LoginEditor
            loginSuccessefull={user => {
              appStateStore.updateUserName(user);
              localStorage.setItem("username", user);
            }}
            showUserRegister={() => {
              appStateStore.updateShowRegister(true);
            }}
          />
        )}
        {appStateStore.state === AppState.SHOWING_REGISTER && (
          <UserRegisterEditor
            returnToLoginClick={() => {
              appStateStore.updateShowRegister(false);
            }}
          />
        )}
      </div>
    );
  }
}
