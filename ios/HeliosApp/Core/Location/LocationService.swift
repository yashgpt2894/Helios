import Foundation
import CoreLocation
import Observation

// MARK: - LocationService — default + browser location

@Observable
final class LocationService: NSObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private var continuation: CheckedContinuation<LocationInfo, Error>?

    var currentLocation: LocationInfo
    var authorizationStatus: CLAuthorizationStatus = .notDetermined

    override init() {
        self.currentLocation = LocationService.defaultLocation()
        super.init()
        manager.delegate = self
        authorizationStatus = manager.authorizationStatus
    }

    static func defaultLocation() -> LocationInfo {
        return LocationInfo(lat: 37.7749, lng: -122.4194, label: "San Francisco, CA", source: .default)
    }

    // MARK: - Request location

    func requestLocation() async throws -> LocationInfo {
        switch manager.authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways:
            return try await withCheckedThrowingContinuation { cont in
                self.continuation = cont
                manager.requestLocation()
            }
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
            return try await withCheckedThrowingContinuation { cont in
                self.continuation = cont
            }
        case .denied, .restricted:
            let fallback = Self.defaultLocation()
            currentLocation = fallback
            return fallback
        @unknown default:
            let fallback = Self.defaultLocation()
            currentLocation = fallback
            return fallback
        }
    }

    // MARK: - Reverse geocode

    static func reverseGeocode(lat: Double, lng: Double) async -> String {
        let geocoder = CLGeocoder()
        let loc = CLLocation(latitude: lat, longitude: lng)
        do {
            let marks = try await geocoder.reverseGeocodeLocation(loc)
            if let place = marks.first {
                if let city = place.locality, let state = place.administrativeArea {
                    return "\(city), \(state)"
                }
                return place.locality ?? place.country ?? "Unknown"
            }
        } catch {}
        return String(format: "%.3f, %.3f", lat, lng)
    }

    // MARK: - CLLocationManagerDelegate

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.first else { return }
        let info = LocationInfo(
            lat: loc.coordinate.latitude,
            lng: loc.coordinate.longitude,
            label: String(format: "%.3f, %.3f", loc.coordinate.latitude, loc.coordinate.longitude),
            source: .browser
        )
        currentLocation = info
        continuation?.resume(returning: info)
        continuation = nil
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        continuation?.resume(throwing: error)
        continuation = nil
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        authorizationStatus = manager.authorizationStatus
        if authorizationStatus == .authorizedWhenInUse || authorizationStatus == .authorizedAlways {
            manager.requestLocation()
        }
    }
}
