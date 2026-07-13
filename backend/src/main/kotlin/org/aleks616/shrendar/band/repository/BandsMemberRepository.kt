package org.aleks616.shrendar.band.repository

import org.aleks616.shrendar.band.model.BandsMembers
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BandsMemberRepository :JpaRepository<BandsMembers,Int>