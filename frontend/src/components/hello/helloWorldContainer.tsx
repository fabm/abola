import {connect} from 'react-redux';
import {State} from '../../reducers';

import {HelloWorldComponent} from './helloWorld';
import { createUserRequest, loginRequest } from '../../actions/userRequest';

const mapStateToProps = (state : State) => {
  return {
    userName: state.userProfileReducer.firstname
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    createUser: () => dispatch(createUserRequest()),
    login:()=> dispatch(loginRequest())
  };
}

export const HelloWorldContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(HelloWorldComponent);
