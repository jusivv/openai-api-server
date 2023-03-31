function post(action = "", data = {}) {
    return new Promise((resolve, reject) => {
      fetch(action, {
          method: "POST",
          headers: {
              "Content-Type": "application/json"
          },
          body: JSON.stringify(data)
      }).then(response => {
          if (response.ok) {
              response.json().then(data => resolve(data))
          } else {
              response.json().then(data => reject(data.status + ": " + data.error))
          }
      }).catch(err => {
          reject(err)
      })
    })
    // const response = await fetch(action, {
    //     method: "POST",
    //     headers: {
    //         "Content-Type": "application/json"
    //     },
    //     body: JSON.stringify(data)
    // })
    //
    // return response.json()
}

export {post}