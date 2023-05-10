<template>
    <h1>Please login</h1>
    <a-form :model="formState" class="login-form">
        <a-form-item
                label="Username"
                name="username"
                :rules="[{ required: true, message: 'Please input your username!' }]"
        >
            <a-input v-model:value="formState.username">
                <template #prefix>
                    <UserOutlined class="site-form-item-icon" />
                </template>
            </a-input>
        </a-form-item>
        <a-form-item
                label="Password"
                name="password"
                :rules="[{ required: true, message: 'Please input your password!' }]"
        >
            <a-input-password v-model:value="formState.password">
                <template #prefix>
                    <LockOutlined class="site-form-item-icon" />
                </template>
            </a-input-password>
        </a-form-item>
        <a-form-item>
            <a-form-item name="remember" no-style>
                <a-checkbox v-model:checked="formState.autoLogin">Remember me in [[${@appConfig.tokenExpireDays}]] days</a-checkbox>
            </a-form-item>
        </a-form-item>
        <a-form-item>
            <a-button :disabled="disabled" type="primary" class="login-form-button" @click="login">
                Login
            </a-button>
        </a-form-item>
    </a-form>
</template>
<style scoped>

</style>
<script>
import { post } from "/app/request.js"
export default {
    name: "Login",
    data() {
        return {
            formState: {
                username: "",
                password: "",
                autoLogin: true
            }
        }
    },
    computed: {
        disabled() {
            const { formState } = this
            return !formState.username || !formState.password
        }
    },
    mounted() {
        if (sessionStorage.sessionId || localStorage.token) {
            this.$router.push("/chat")
        }
    },
    methods: {
        login() {
            const { username, password, autoLogin } = this.formState

            if (username && password) {
                post("/account/login", {
                    name: username,
                    pass: password,
                    autoLogin
                }).then(data => {
                    sessionStorage.setItem("sessionId", data.sessionId)
                    if (data.token) {
                        localStorage.setItem("token", data.token)
                    }
                    // redirect
                    this.$router.push("/chat")
                }).catch(err=> {
                    alert(err)
                })
            }
        }
    }
}
</script>