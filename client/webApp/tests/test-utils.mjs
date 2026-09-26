import React from "react"
import {renderToStaticMarkup} from "react-dom/server"
import react from "@vitejs/plugin-react"
import {createServer} from "vite"
import {fileURLToPath} from "node:url"

const webAppRoot=fileURLToPath(new URL("..",import.meta.url))

const testMocks={
    name: "shrendar-component-test-mocks",
    resolveId(source){
        if(source==="sharedLogic") return "\0shrendar-test-shared-logic"
        if(source==="sharedLogic/localization/comexampleclient_stringsJson.json"){
            return "\0shrendar-test-english-strings"
        }
        if(source==="sharedLogic/localization/comexampleclient_stringsJson_pl.json"){
            return "\0shrendar-test-polish-strings"
        }
        if(source.endsWith("getLanguage.ts")) return "\0shrendar-test-language"
    },
    load(id){
        if(id==="\0shrendar-test-shared-logic"){
            return `
        export class RegisterValidator {
          async validateLogin() { return null }
          async validateEmail() { return null }
          isPasswordValid() { return true }
        }
        export class RegisterRequestDto {}
        export class LoginRequestDto {}
        export const RegisterClient = {
          getInstance: () => ({
            register: async () => "verification_code_sent",
            registerConfirm: async () => "account_created"
          })
        }
        export const AccountClient = {
          getInstance: () => ({
            login: async () => JSON.stringify({ token: "test-token" }),
            logout: async () => "logged_out"
          })
        }
      `
        }

        if(id==="\0shrendar-test-language"){
            return 'export function getLanguage() { return "EN" }'
        }

        if(id==="\0shrendar-test-english-strings"){
            return `export default ${JSON.stringify({
                logout: "Log out",
                create_account: "Create account",
                sign_up_to_continue: "Sign Up to Continue",
                email_address: "E-mail address",
                login: "Login",
                password: "Password",
                re_enter_password: "Repeat password",
                sign_up: "Sign Up",
                or: "or",
                special_sign_in_later: "You can sign in later",
                sign_in: "Sign in",
                login_email: "Login or e-mail address",
            })}`
        }

        if(id==="\0shrendar-test-polish-strings"){
            return "export default {}"
        }
    }
}

export async function createComponentTestServer(){
    return createServer({
        configFile: false,
        root: webAppRoot,
        plugins: [react(),testMocks],
        server: {middlewareMode: true},
        appType: "custom",
        logLevel: "error",
    })
}

export async function renderComponent(server,modulePath,exportName,props={}){
    const componentModule=await server.ssrLoadModule(modulePath)
    return renderToStaticMarkup(
        React.createElement(componentModule[exportName],props),
    )
}
