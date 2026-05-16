import SwiftUI

// MARK: - EnergyFlowView — Canvas + TimelineView animated energy flow diagram

struct EnergyFlowView: View {
    let telemetry: SolarTelemetry
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        TimelineView(.animation(minimumInterval: 1 / 60, paused: false)) { timeline in
            Canvas { context, size in
                drawEnergyFlow(context: context, size: size, date: timeline.date)
            }
        }
    }

    // MARK: - Drawing

    private func drawEnergyFlow(context: GraphicsContext, size: CGSize, date: Date) {
        let w = size.width
        let h = size.height
        let cx = w / 2
        let cy = h / 2 + 8
        let hubR: CGFloat = 44

        let solarPos = CGPoint(x: cx, y: cy - 116)
        let batteryPos = CGPoint(x: cx - 116, y: cy + 86)
        let gridPos = CGPoint(x: cx + 116, y: cy + 86)
        let homePos = CGPoint(x: cx, y: cy)

        let solarActive = telemetry.acPowerW > 30
        let batteryCharging = telemetry.batteryPowerW > 30
        let batteryDischarging = telemetry.batteryPowerW < -30
        let gridImporting = telemetry.gridImportW > 30
        let gridExporting = telemetry.gridExportW > 30

        // Draw connections
        drawConnection(context: context, from: solarPos, to: homePos, active: solarActive, color: solarColor, hubR: hubR, nodeR: 28, date: date, wattage: telemetry.acPowerW)
        drawConnection(context: context, from: batteryPos, to: homePos, active: batteryCharging || batteryDischarging, reverse: batteryCharging, color: batteryColor, hubR: hubR, nodeR: 28, date: date, wattage: abs(telemetry.batteryPowerW))
        drawConnection(context: context, from: gridPos, to: homePos, active: gridImporting || gridExporting, reverse: gridExporting, color: gridColor, hubR: hubR, nodeR: 28, date: date, wattage: max(telemetry.gridImportW, telemetry.gridExportW))

        // Draw hub glow
        if solarActive {
            let glow = Path(ellipseIn: CGRect(x: cx - hubR - 18, y: cy - hubR - 18, width: (hubR + 18) * 2, height: (hubR + 18) * 2))
            context.fill(glow, with: .color(solarColor.opacity(0.15)))
        }

        // Draw hub
        drawNode(context: context, center: homePos, radius: hubR, icon: "house", label: "Home", value: Format.formatW(telemetry.homeLoadW), active: telemetry.homeLoadW > 30, accent: flowColor)

        // Draw nodes
        drawNode(context: context, center: solarPos, radius: 28, icon: "sun.max", label: "Solar", value: Format.formatW(telemetry.acPowerW), active: solarActive, accent: solarColor)
        drawNode(context: context, center: batteryPos, radius: 28, icon: "battery.100", label: "Battery", value: Format.formatW(abs(telemetry.batteryPowerW)), active: batteryCharging || batteryDischarging, accent: batteryColor)
        drawNode(context: context, center: gridPos, radius: 28, icon: "powerplug", label: "Grid", value: Format.formatW(max(telemetry.gridImportW, telemetry.gridExportW)), active: gridImporting || gridExporting, accent: gridColor)
    }

    // MARK: - Connection

    private func drawConnection(context: GraphicsContext, from: CGPoint, to: CGPoint, active: Bool, reverse: Bool = false, color: Color, hubR: CGFloat, nodeR: CGFloat, date: Date, wattage: Double) {
        let start = reverse ? to : from
        let end = reverse ? from : to
        let dx = end.x - start.x
        let dy = end.y - start.y
        let length = hypot(dx, dy)
        guard length > 0 else { return }
        let ux = dx / length
        let uy = dy / length
        let padStart = hubR + 4
        let padEnd = nodeR + 10
        let sx = start.x + ux * padStart
        let sy = start.y + uy * padStart
        let ex = end.x - ux * padEnd
        let ey = end.y - uy * padEnd

        // Quiet base line
        var basePath = Path()
        basePath.move(to: CGPoint(x: sx, y: sy))
        basePath.addLine(to: CGPoint(x: ex, y: ey))
        context.stroke(basePath, with: .color(HeliosColor.separatorHairline(for: colorScheme)), lineWidth: 1)

        guard active else { return }

        // Active dashed line
        var dashPath = Path()
        dashPath.move(to: CGPoint(x: sx, y: sy))
        dashPath.addLine(to: CGPoint(x: ex, y: ey))
        context.stroke(dashPath, with: .color(color.opacity(0.7)), lineWidth: 1.5, style: StrokeStyle(dash: [2, 6]))

        // Animated dots
        let dotCount = dotCountForWattage(wattage)
        let speed = dotSpeedForWattage(wattage)
        let phase = fmod(date.timeIntervalSinceReferenceDate, speed) / speed

        for i in 0..<dotCount {
            let t = fmod(phase + Double(i) / Double(dotCount), 1.0)
            let px = sx + (ex - sx) * CGFloat(t)
            let py = sy + (ey - sy) * CGFloat(t)
            let dot = Path(ellipseIn: CGRect(x: px - 2.5, y: py - 2.5, width: 5, height: 5))
            context.fill(dot, with: .color(color))
            // Glow
            let glow = Path(ellipseIn: CGRect(x: px - 6, y: py - 6, width: 12, height: 12))
            context.fill(glow, with: .color(color.opacity(0.3)))
        }
    }

    // MARK: - Node

    private func drawNode(context: GraphicsContext, center: CGPoint, radius: CGFloat, icon: String, label: String, value: String, active: Bool, accent: Color) {
        // Outer ring
        let outer = Path(ellipseIn: CGRect(x: center.x - radius - 8, y: center.y - radius - 8, width: (radius + 8) * 2, height: (radius + 8) * 2))
        context.stroke(outer, with: .color(HeliosColor.separatorHairline(for: colorScheme)), lineWidth: 1)

        // Fill
        let fill = Path(ellipseIn: CGRect(x: center.x - radius, y: center.y - radius, width: radius * 2, height: radius * 2))
        context.fill(fill, with: .color(HeliosColor.nodeFill(for: colorScheme)))
        context.stroke(fill, with: .color(active ? accent : HeliosColor.nodeStroke(for: colorScheme)), lineWidth: active ? 1.5 : 1)

        // We can't draw SF Symbols in Canvas directly; draw label below
        let labelText = Text(label)
            .font(.system(size: 9, weight: .medium, design: .monospaced))
            .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
        let resolvedLabel = context.resolve(labelText)
        context.draw(resolvedLabel, at: CGPoint(x: center.x, y: center.y + radius + 24))

        let valueText = Text(value)
            .font(.system(size: 11, weight: .medium, design: .monospaced))
            .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
        let resolvedValue = context.resolve(valueText)
        context.draw(resolvedValue, at: CGPoint(x: center.x, y: center.y + radius + 40))
    }

    // MARK: - Helpers

    private func dotCountForWattage(_ w: Double) -> Int {
        switch w {
        case 0..<1: return 0
        case 1..<500: return 1
        case 500..<2000: return 2
        case 2000..<5000: return 3
        default: return 4
        }
    }

    private func dotSpeedForWattage(_ w: Double) -> Double {
        let base: Double = 2.2
        let minSpeed: Double = 1.0
        let ratio = min(w / 10000, 1.0)
        return base - (base - minSpeed) * ratio
    }

    private var solarColor: Color { HeliosColor.solar(for: colorScheme) }
    private var batteryColor: Color { HeliosColor.battery(for: colorScheme) }
    private var gridColor: Color { HeliosColor.grid(for: colorScheme) }
    private var flowColor: Color { HeliosColor.flow(for: colorScheme) }
}

// MARK: - Preview

#Preview {
    EnergyFlowView(telemetry: TelemetryRepository().telemetry)
        .frame(height: 360)
        .preferredColorScheme(.dark)
}
