---
id: task-003
title: Add real-time amplitude visualization during recording
status: Done
assignee: []
created_date: '2025-07-19'
updated_date: '2025-07-28'
labels: []
dependencies: []
---

## Description

Provide visual feedback during recording with dynamic waveform or pulsing animation. This creates an engaging and informative recording experience.

## Acceptance Criteria

- [ ] AudioRecorder interface exposes Flow of amplitude values
- [ ] Android implementation uses MediaRecorder.maxAmplitude
- [ ] iOS implementation uses AVAudioRecorder.averagePower
- [ ] Recording screen displays real-time visual feedback
- [ ] Animation is smooth and responsive to audio input
- [ ] Visual feedback works on both Android and iOS

## Implementation Notes

Real-time amplitude visualization implemented with AudioWaveformExtractor and AmplitudeCollector integration
