package com.techone.domain.user.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "districts")
public class District {
    @Id
    @Column(name = "id")
    public Integer id;

    @Column(columnDefinition = "varchar(255)")
    public String code;

    @Column(columnDefinition = "nvarchar(255)")
    public String name;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "province_id")
    Province province;

    @JsonIgnore
    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
    public List<Ward> ward;
}
