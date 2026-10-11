//
//  KeychainService.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/09/2026.
//


import Security
import SwiftUI

enum KeychainService {
	private static let account = "userAccount"

	@discardableResult
	static func saveToken(_ token: String) -> Bool {
		let query: [String: Any] = [
			kSecClass as String: kSecClassGenericPassword,
			kSecAttrAccount as String: account,
			kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlocked,
			kSecUseDataProtectionKeychain as String: true,
			kSecValueData as String: Data(token.utf8)
		]

		return SecItemAdd(query as CFDictionary, nil) == errSecSuccess
	}

	static func retrieveToken() -> String? {
		let query: [String: Any] = [
			kSecClass as String: kSecClassGenericPassword,
			kSecAttrAccount as String: account,
			kSecUseDataProtectionKeychain as String: true,
			kSecReturnData as String: true,
			kSecMatchLimit as String: kSecMatchLimitOne
		]

		var result: AnyObject?

		guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess,
			  let data = result as? Data else {
			return nil
		}

		return String(data: data, encoding: .utf8)
	}
	
	static func removeToken() -> Bool {
		let query: [String: Any] = [
			kSecClass as String: kSecClassGenericPassword,
			kSecAttrAccount as String: account,
			kSecUseDataProtectionKeychain as String: true
		]
		
		let status = SecItemDelete(query as CFDictionary)
		
		return status == errSecSuccess || status == errSecItemNotFound
	}
}
