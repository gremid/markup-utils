/*
 * Copyright © 2017 Gregor Middell (http://gregor.middell.net/)
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
 */
package net.middell;

import java.util.Objects;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class OffsetRange {

    public final long from;
    public final long to;

    public OffsetRange(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof OffsetRange) {
            final OffsetRange other = (OffsetRange) obj;
            return from == other.from && to == other.to;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("[%7d, %7d]", from, to);
    }
}
