//
//  RegisterView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 16/09/2026.
//

import SwiftUI
import GoogleSignIn
import GoogleSignInSwift
import AuthenticationServices

struct RegisterView: View {
   @State private var email:String = ""
   @State private var login:String = ""
   @State private var password:String = ""
   
    var body: some View {
       VStack(){
          Text("Create account")
             .font(.system(size: 28.0,weight:.bold))
          Text("Sign Up to Continue")
             .font(.system(size: 20.0))
          Divider()
          Spacer()
          Form{
             TextField("example@gmail.com", text: $email)
                .keyboardType(.emailAddress)
                .textContentType(.emailAddress)
                .font(.system(size: 24.0))
             
             TextField("login",text: $login)
                .textContentType(.username)
                .font(.system(size: 24.0))
             
             SecureField("password",text:$password)
                .textContentType(.password)
                .font(.system(size: 24.0))
          }
          .frame(maxHeight: 800)
          
          Button("Create account"){
          }.disabled(email.isEmpty||login.isEmpty||password.isEmpty)
          LabelledDivider(label: "or")
          GoogleSignInButton(scheme: .light,
                             state: .normal,
                             action: {handleGoogleSignInButton()})
          .frame(width: 280, height: 45)
          
          SignInWithAppleButton(.continue){
             request in request.requestedScopes=[.email]
          } onCompletion: { result in
             switch result {
                 case .success(let authorization):
                     print("Authorization successful: \(authorization)")
                 case .failure(let error):
                     print("Authorization failed: \(error.localizedDescription)")
                 }
          }.frame(width: 280, height: 45)
          
          Spacer()
          HStack{
             Text("Already have an account?")
             Button("Sign in"){}
          }
          
          
       }
    }
   func handleGoogleSignInButton() {
     guard let rootViewController = UIApplication.shared.rootViewController else {
       // Handle error
       return
     }
      
      GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { signInResult, error in
         guard error == nil else { print(error ?? "none");return}
         guard let signInResult = signInResult else { return }

         let user = signInResult.user
         //let emailAddress = user.profile?.email
         signInResult.user.refreshTokensIfNeeded { user, error in
                 guard error == nil else { return }
                 guard let user = user else { return }

                 let idToken = user.idToken
                  print(idToken)
                 // Send ID token to backend (example below).
             }
      }
   }
}

#Preview {
   RegisterView()
}
