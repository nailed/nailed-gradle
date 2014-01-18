package jk_5.nailed.gradle.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * No description given
 *
 * @author jk-5
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pair <K, V> {
    private K key;
    private V value;
}
