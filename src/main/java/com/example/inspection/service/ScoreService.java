package com.example.inspection.service;

import com.example.inspection.dto.ReportDtos.AutoPayload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ScoreService {

    public ScoreResult score(AutoPayload a, Map<String, String> manual) {
        int score = 100;
        List<String> warnings = new ArrayList<>();

        if (Boolean.TRUE.equals(a.emulatorLikely())) {
            score -= 40;
            warnings.add("疑似模拟器环境");
        }
        if (Boolean.TRUE.equals(a.suBinaryFound())) {
            score -= 25;
            warnings.add("检测到 su 二进制");
        }
        if (Boolean.TRUE.equals(a.testKeysBuild())) {
            score -= 15;
            warnings.add("系统为 test-keys 构建");
        }
        if (manual != null) {
            long fail = manual.values().stream().filter("FAIL"::equals).count();
            score -= (int) fail * 10;
            if (fail > 0) warnings.add(fail + " 个手动检测项未通过");
        }

        score = Math.max(0, score);
        String verdict = score >= 80 ? "PASS" : score >= 50 ? "WARNING" : "FAIL";
        String summary = warnings.isEmpty() ? "设备状态良好" : String.join("; ", warnings);
        return new ScoreResult(score, verdict, summary, warnings);
    }

    public record ScoreResult(int score, String verdict, String summary, List<String> warnings) {}
}