function post(action = "", data = {}) {
    return new Promise((resolve, reject) => {
      fetch(action, {
          method: "POST",
          headers: {
              "Content-Type": "application/json",
              "X-SESSION-ID": sessionStorage.sessionId || "",
              "X-TOKEN-ID": localStorage.token || ""
          },
          body: JSON.stringify(data)
      }).then(response => {
          if (response.ok) {
              response.json().then(data => resolve(data))
          } else {
              response.json().then(data => reject(data.code + ": " + data.message))
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

function get(action) {
    return new Promise((resolve, reject) => {
        fetch(action, {
            method: "GET",
            headers: {
                "X-SESSION-ID": sessionStorage.sessionId || "",
                "X-TOKEN-ID": localStorage.token || ""
            }
        }).then(response => {
            if (response.ok) {
                response.json().then(data => resolve(data))
            } else {
                response.json().then(data => reject(data.code + ": " + data.message))
            }
        }).catch(err => {
            reject(err)
        })
    })
}

export { post, get }