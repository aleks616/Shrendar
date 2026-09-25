//
//  UIApplication.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 16/09/2026.
//

import UIKit

extension UIApplication {
  // Minimal implementation to retrieve the active root view
  // controller for presentation. Apps presenting sign-in from deeper
  // within an existing view hierarchy should ensure they select the
  // appropriate view controller.
  var rootViewController: UIViewController? {
    let windowScene = connectedScenes
      .compactMap { scene in scene as? UIWindowScene }
      .first { scene in scene.activationState == .foregroundActive }
    return windowScene?.windows.first(where: { window in window.isKeyWindow })?.rootViewController
  }
}
