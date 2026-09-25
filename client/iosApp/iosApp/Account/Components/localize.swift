//
//  localize.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 24/09/2026.
//
import SharedLogic
import SwiftUI

func localize(key: String) -> String {
    return LocalText().getStringDesc(key: key).localized()
  }
