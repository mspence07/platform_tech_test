package com.claimline.policy;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ApprovalRequirements {

    private static final Gson GSON = new Gson();

    @SerializedName("approvalThresholds")
    private final List<Threshold> thresholds;

    public ApprovalRequirements(final List<Threshold> thresholds) {
        this.thresholds = new ArrayList<>(thresholds);
    }

    public int approvalsRequiredFor(long amount) {
        Threshold bestMatch = null;

        for (Threshold threshold : thresholds) {
            boolean amountMatches = amount >= threshold.minimumAmount();
            boolean isBetterMatch = bestMatch == null
                    || threshold.minimumAmount() > bestMatch.minimumAmount();

            if (amountMatches && isBetterMatch) {
                bestMatch = threshold;
            }
        }

        if (null == bestMatch) {
            throw new IllegalStateException(
                    "no approval threshold configured for amount " + amount);
        }

        return bestMatch.approvalsRequired();
    }

    public static ApprovalRequirements fromJson(final Path jsonFile) {
        try (Reader reader = Files.newBufferedReader(
                jsonFile, StandardCharsets.UTF_8)) {

            return GSON.fromJson(reader, ApprovalRequirements.class);

        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to read the file: " + jsonFile, exception);
        } catch (JsonParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid JSON in file: " + jsonFile, exception);
        }
    }




}
