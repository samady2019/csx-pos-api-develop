package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "language")
public class LanguageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 255)
    private String code;

    @Lob
    @Column(name = "khmer")
    private String khmer;

    @Lob
    @Column(name = "english")
    private String english;

    @Lob
    @Column(name = "chinese")
    private String chinese;

    @Lob
    @Column(name = "thai")
    private String thai;

    @Lob
    @Column(name = "vietnamese")
    private String vietnamese;
}
