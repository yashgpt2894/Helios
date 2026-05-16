import ClockKit
import SwiftUI

// MARK: - ComplicationTimelineProvider

struct ComplicationTimelineProvider: TimelineProvider {

    // MARK: - Placeholder

    func placeholder(in context: CLKComplicationContext) -> ComplicationEntry {
        ComplicationEntry(date: Date(), currentKW: 4.2, batterySOC: 78)
    }

    // MARK: - Snapshot

    func getSnapshot(in context: CLKComplicationContext, completion: @escaping (ComplicationEntry) -> Void) {
        let controller = HeliosComplicationController.shared
        let entry = ComplicationEntry(
            date: Date(),
            currentKW: controller.currentKW(),
            batterySOC: controller.currentBatterySOC()
        )
        completion(entry)
    }

    // MARK: - Timeline

    func getTimeline(in context: CLKComplicationContext, completion: @escaping (Timeline<ComplicationEntry>) -> Void) {
        let controller = HeliosComplicationController.shared
        let entry = ComplicationEntry(
            date: Date(),
            currentKW: controller.currentKW(),
            batterySOC: controller.currentBatterySOC()
        )

        let refreshDate = Calendar.current.date(
            byAdding: .minute,
            value: 15,
            to: Date()
        ) ?? Date().addingTimeInterval(15 * 60)

        let timeline = Timeline(entries: [entry], policy: .after(refreshDate))
        completion(timeline)
    }
}
