import SwiftUI

// MARK: - BrandService — port of services/brand.ts

enum BrandService {
    static let HELIOS: BrandInfo = BrandInfo(
        id: "helios",
        name: "Helios",
        legalName: "Helios Energy Inc.",
        accent: "#FFCA28",
        accentLight: "#FFF176",
        mark: "helios",
        textMark: nil,
        supportEmail: "support@helios.energy",
        supportUrl: "https://helios.energy/support",
        tagline: "Energy clarity."
    )

    static let BRANDS: [String: BrandInfo] = [
        "helios": HELIOS,
        "solarius": BrandInfo(
            id: "solarius",
            name: "Solarius",
            legalName: "Solarius Power LLC",
            accent: "#FF6F00",
            accentLight: "#FFB300",
            mark: "text",
            textMark: "S",
            supportEmail: "help@solarius-solar.com",
            supportUrl: "https://solarius-solar.com/help",
            tagline: "Rise with the sun."
        ),
        "ohmio": BrandInfo(
            id: "ohmio",
            name: "Ohmio",
            legalName: "Ohmio Energy Corp.",
            accent: "#00E676",
            accentLight: "#69F0AE",
            mark: "text",
            textMark: "O",
            supportEmail: "support@ohmio.io",
            supportUrl: "https://ohmio.io/support",
            tagline: "Smart energy living."
        ),
        "voltix": BrandInfo(
            id: "voltix",
            name: "Voltix",
            legalName: "Voltix Inc.",
            accent: "#536DFE",
            accentLight: "#82B1FF",
            mark: "text",
            textMark: "V",
            supportEmail: "care@voltix.de",
            supportUrl: "https://voltix.de/hilfe",
            tagline: "Energie neu gedacht."
        )
    ]

    static func resolveBrand(id: String) -> BrandInfo {
        return BRANDS[id] ?? HELIOS
    }

    static func brandFromSearch(_ query: String) -> BrandInfo? {
        let lower = query.lowercased().trimmingCharacters(in: .whitespaces)
        return BRANDS.values.first {
            $0.name.lowercased().contains(lower) || ($0.legalName?.lowercased().contains(lower) ?? false)
        }
    }

    static func applyBrandAccent(_ brand: BrandInfo, to context: inout [String: Any]) {
        context["accent"] = brand.accent
        context["accentLight"] = brand.accentLight
        context["brandId"] = brand.id
        context["brandName"] = brand.name
        context["supportEmail"] = brand.supportEmail ?? "support@helios.energy"
        context["supportUrl"] = brand.supportUrl ?? "https://helios.energy/support"
        context["tagline"] = brand.tagline ?? ""
    }
}
