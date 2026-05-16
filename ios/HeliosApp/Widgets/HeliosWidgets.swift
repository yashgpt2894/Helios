import WidgetKit
import SwiftUI

// MARK: - HeliosWidgetBundle

@main
struct HeliosWidgets: WidgetBundle {
    var body: some Widget {
        SmallWidget()
        MediumWidget()
        LargeWidget()
        LockScreenCircularWidget()
        LockScreenRectangularWidget()
        LockScreenInlineWidget()
    }
}
