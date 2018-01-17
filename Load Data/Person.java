package com.blu.imdg.jdbc.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.ignite.cache.affinity.AffinityKey;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.cache.query.annotations.QueryTextField;

public class Person implements Serializable {
    /** */
    private static final AtomicLong ID_GEN = new AtomicLong();

    /** Person ID (indexed). */
    @QuerySqlField(index = true)
    public Long id;

    /** Organization ID (indexed). */
    @QuerySqlField(index = true)
    public Long orgId;

    /** First name (not-indexed). */
    @QuerySqlField
    public String firstName;

    /** Last name (not indexed). */
    @QuerySqlField
    public String lastName;

    /** Resume text (create LUCENE-based TEXT index for this field). */
    @QueryTextField
    public String resume;

    /** Salary (indexed). */
    @QuerySqlField(index = true)
    public double salary;

    /** Custom cache key to guarantee that person is always collocated with its organization. */
    private transient AffinityKey<Long> key;

    /**
     * Default constructor.
     */
    public Person() {
        // No-op.
    }

    /**
     * Constructs person record.
     *
     * @param org       Organization.
     * @param firstName First name.
     * @param lastName  Last name.
     * @param salary    Salary.
     * @param resume    Resume text.
     */
    public Person(Organization org, String firstName, String lastName, double salary, String resume) {
        // Generate unique ID for this person.
        id = ID_GEN.incrementAndGet();

        orgId = org.id();

        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.resume = resume;
    }

    /**
     * Constructs person record.
     *
     * @param id Person ID.
     * @param orgId Organization ID.
     * @param firstName First name.
     * @param lastName Last name.
     * @param salary    Salary.
     * @param resume    Resume text.
     */
    public Person(Long id, Long orgId, String firstName, String lastName, double salary, String resume) {
        this.id = id;
        this.orgId = orgId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.resume = resume;
    }

    /**
     * Constructs person record.
     *
     * @param id Person ID.
     * @param firstName First name.
     * @param lastName Last name.
     */
    public Person(Long id, String firstName, String lastName) {
        this.id = id;

        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Gets cache affinity key. Since in some examples person needs to be collocated with organization, we create
     * custom affinity key to guarantee this collocation.
     *
     * @return Custom affinity key to guarantee that person is always collocated with organization.
     */
    public AffinityKey<Long> key() {
        if (key == null)
            key = new AffinityKey<>(id, orgId);

        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override public String toString() {
        return "Person [id=" + id +
                ", orgId=" + orgId +
                ", lastName=" + lastName +
                ", firstName=" + firstName +
                ", salary=" + salary +
                ", resume=" + resume + ']';
    }
}
