/**
 * STUDY — Shared API shapes reused across feature controllers.
 *
 * <p><b>What lives here:</b> pagination wrappers and other response envelopes that are not
 * owned by a single domain (item, inventory, …).
 *
 * <p><b>Why shared?</b> Controllers return the same JSON page shape so the React UI can
 * reuse one table/pagination pattern. Keep domain DTOs inside their feature packages.
 *
 * <p>Start with {@link com.pl.warehouse.common.api.PageResponse}.
 */
package com.pl.warehouse.common.api;
