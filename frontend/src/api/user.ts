class UserAPI {

  doLogin():Promise<any>{
    const userLoginApi = "/api/user/login"
    return fetch(userLoginApi,  {method: 'post',
    headers: {
      'Accept': 'application/json, text/plain, */*',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      'user':'xico',
      'pass':'minhapassword'
    })}).then((response)=>{
      console.log(response);
    })
  }

  createUser():Promise<any>{
    const userLoginApi = "/api/user"
    return fetch(userLoginApi,  {method: 'post',
    headers: {
      'Accept': 'application/json, text/plain, */*',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      'name':'xico',
      'email':'francisco@mail.com',
      'password':'minhapassword'
    })}).then((response)=>{
      console.log(response);
    })
  }


}

export const userAPI = new UserAPI();