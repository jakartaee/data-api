/*
 * Copyright (c) 2022,2023 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package jakarta.data.displaynamegeneration.test;

import jakarta.data.displaynamegeneration.ReplaceCamelCaseAndUnderscore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayNameGeneration(ReplaceCamelCaseAndUnderscore.class)
class ReplaceCamelCaseAndUnderscoreTest {
    private ReplaceCamelCaseAndUnderscore replaceCamelCaseAndUnderscore;

    @BeforeEach
    void setUp() {
        replaceCamelCaseAndUnderscore = new ReplaceCamelCaseAndUnderscore();
    }

    @Test
    void shouldManageCamelCaseAndUnderscoreVeryWell() {
        String input1 = "shouldReturnErrorWhen_maxResults_IsNegative";
        String result1 = replaceCamelCaseAndUnderscore.replaceCamelCaseAndUnderscore(input1);
        String input2 = "shouldCreateLimitWithRange";
        String result2 = replaceCamelCaseAndUnderscore.replaceCamelCaseAndUnderscore(input2);
        assertSoftly(softly -> {
            softly.assertThat(result1).isEqualTo("Should return error when maxResults is negative");
            softly.assertThat(result2).isEqualTo("Should create limit with range");
        });
    }
}